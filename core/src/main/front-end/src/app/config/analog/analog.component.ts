import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ActionsService, AnalogAction} from '../../service/actions.service';
import {ActivatedRoute} from '@angular/router';
import {combineAllParams} from '../../../helper';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {HttpClient} from '@angular/common/http';
import {Observable, Subject} from 'rxjs';
import {debounceTime, switchMap} from 'rxjs/operators';

@UntilDestroy()
@Component({
  selector: 'pcp-analog',
  templateUrl: './analog.component.html',
  styleUrls: ['./analog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AnalogComponent {
  selected?: AnalogAction;
  toUpdate = new Subject<any>();

  constructor(public actionsService: ActionsService,
              private http: HttpClient,
              route: ActivatedRoute) {
    let device = '';
    let type = '';
    let number = '0';
    combineAllParams(route).pipe(untilDestroyed(this)).subscribe(ps => {
      device = ps.device;
      type = ps.type;
      number = ps.number;
    });

    this.toUpdate.pipe(untilDestroyed(this), debounceTime(500), switchMap(val => this.save(device, type, number, val))).subscribe(val => {
      console.log('Update', val);
    });
  }

  private save(device: string, control: string, idx: string, params: any): Observable<any> {
    return this.http.post(`api/changeanalog`, {device, control, idx, params});
  }
}
