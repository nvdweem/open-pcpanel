import {ChangeDetectionStrategy, Component} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {combineAllParams} from '../../helper';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {Device, DeviceService} from '../service/device.service';
import {Observable} from 'rxjs';
import {distinctUntilChanged, map, switchMap} from 'rxjs/operators';
import {ClickEvent} from './pro/pro.component';

@UntilDestroy()
@Component({
  selector: 'pcp-device',
  templateUrl: './device.component.html',
  styleUrls: ['./device.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DeviceComponent {
  device = '';
  deviceObject$: Observable<Device>;

  constructor(private http: HttpClient,
              public deviceService: DeviceService,
              private route: ActivatedRoute,
              private router: Router) {
    this.deviceObject$ = combineAllParams(route).pipe(
      distinctUntilChanged((l, r) => l.device === r.device),
      untilDestroyed(this),
      map(pm => {
        this.device = pm.device;
        http.put(`api/profile/${pm.device}/${pm.profile}`, {}).subscribe();
        deviceService.reload();
        return this.device;
      }),
      switchMap(d => deviceService.device$(d))
    );
    this.deviceObject$.subscribe();
  }

  save() {
    this.http.post(`api/profile/${this.device}/save`, {}).subscribe();
  }

  navigateTo($event: ClickEvent): Promise<boolean> {
    const relativeTo = this.route;
    switch ($event.control) {
      case 'knob':
        return this.router.navigate([`${$event.control}/${$event.idx}/click`], {relativeTo});
      case 'slider':
        return this.router.navigate([`${$event.control}/${$event.idx}/analog`], {relativeTo});
      case 'logo':
      case 'body':
        return this.router.navigate([`${$event.control}/${$event.idx}/light`], {relativeTo});
    }
  }
}
