import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ActionsService, AnalogAction} from '../../service/actions.service';
import {ActivatedRoute} from '@angular/router';
import {combineAllParams} from '../../../helper';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, combineLatest, Observable, Subject} from 'rxjs';
import {debounceTime, map, shareReplay, switchMap, tap} from 'rxjs/operators';
import {DeviceService} from '../../service/device.service';

@UntilDestroy()
@Component({
  selector: 'pcp-analog',
  templateUrl: './analog.component.html',
  styleUrls: ['./analog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AnalogComponent {
  selected = new BehaviorSubject<AnalogAction | undefined>(undefined);
  options: Observable<any>;
  toUpdate = new Subject<any>();

  constructor(public actionsService: ActionsService,
              private http: HttpClient,
              route: ActivatedRoute,
              ds: DeviceService) {
    let device = '';
    let type = '';
    let number = '0';
    let profile = '';
    let idx = 0;
    this.options = combineAllParams(route).pipe(untilDestroyed(this),
      tap(ps => {
        device = ps.device;
        type = ps.type;
        number = ps.number;
        profile = ps.profile;

        idx = Number(number) - 1;
        if (type === 'slider') idx += 4;
      }),
      switchMap(() => combineLatest([ds.device$(device), actionsService.analog])),
      map(([device, aa]) => [device.activeProfile.actionsConfig.analogActions[idx], aa]),
      tap(([a, as]) => this.selected.next(as.find((aa: AnalogAction) => aa.impl === a.actionClass))),
      map(([a]) => a),
      shareReplay(1));
    this.options.subscribe();

    this.toUpdate.pipe(untilDestroyed(this), debounceTime(500), switchMap(val => this.save(device, type, number, val))).subscribe(val => {
      console.log('Update', val);
    });
  }

  private save(device: string, control: string, idx: string, params: any): Observable<any> {
    return this.http.post(`api/changeanalog`, {device, control, idx, params});
  }
}
