import {ChangeDetectionStrategy, Component} from '@angular/core';
import {DeviceService, Profile} from '../service/device.service';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, merge, Observable, Subject} from 'rxjs';
import {filter, map, switchMap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'pcp-device-profile',
  templateUrl: './device-profile.component.html',
  styleUrls: ['./device-profile.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DeviceProfileComponent {
  private newProfile$ = new Subject<string | null>();
  deviceId = '';
  profiles$: Observable<Profile[]>;

  constructor(private http: HttpClient,
              deviceService: DeviceService,
              router: Router,
              route: ActivatedRoute) {
    const newProfile$ = this.newProfile$.pipe(
      filter(n => !!n),
      switchMap(n => this.doNewProfile(n!))
    );

    const initialProfile$ = combineLatest([deviceService.devices$, route.paramMap]).pipe(map(([devices, params]) => {
      this.deviceId = String(params.get('device'));
      const fDevices = devices.filter(d => d.id === this.deviceId);
      if (fDevices.length === 0) {
        console.error(this.deviceId, 'is not a valid device');
        return [];
      }
      const device = fDevices[0];
      device.profiles.sort((l, r) => l.name.localeCompare(r.name));
      if (!route.firstChild && device.profiles.length !== 0) {
        router.navigate([`${device.id}/${device.profiles[0].id}`], {replaceUrl: true});
      }

      return device.profiles;
    }));

    this.profiles$ = merge(initialProfile$, newProfile$);
  }

  newProfile(): void {
    this.newProfile$.next(prompt('Name for new profile'));
  }

  private doNewProfile(name: string): Observable<Profile[]> {
    return this.http.post<Profile[]>(`api/profile/${this.deviceId}`, {}, {params: {name}});
  }
}
