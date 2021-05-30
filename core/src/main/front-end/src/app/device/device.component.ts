import {ChangeDetectionStrategy, Component} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {combineAllParams} from '../../helper';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 'pcp-device',
  templateUrl: './device.component.html',
  styleUrls: ['./device.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DeviceComponent {
  private device = '';

  constructor(private http: HttpClient,
              route: ActivatedRoute) {
    combineAllParams(route).pipe(untilDestroyed(this)).subscribe(pm => {
      this.device = pm.device;
    });
  }

  save() {
    this.http.post(`api/profile/${this.device}/save`, {}).subscribe();
  }
}
