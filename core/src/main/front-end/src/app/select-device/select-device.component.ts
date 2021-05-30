import {ChangeDetectionStrategy, Component} from '@angular/core';
import {Device, DeviceService} from '../service/device.service';
import {take, tap} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {Router} from '@angular/router';

@Component({
  selector: 'pcp-select-device',
  templateUrl: './select-device.component.html',
  styleUrls: ['./select-device.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SelectDeviceComponent {
  devices$: Observable<Device[]>;

  constructor(deviceService: DeviceService, router: Router) {
    this.devices$ = deviceService.devices$.pipe(take(1), tap(ds => {
      if (ds.length > 0) {
        router.navigate([`${ds[0].id}`], {replaceUrl: true});
      }
    }));
  }
}
