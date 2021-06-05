import {ChangeDetectionStrategy, Component} from '@angular/core';
import {Action, ActionsService} from '../../service/actions.service';
import {ActivatedRoute} from '@angular/router';
import {combineAllParams} from '../../../helper';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, combineLatest, Observable, Subject} from 'rxjs';
import {debounceTime, map, shareReplay, switchMap, tap} from 'rxjs/operators';
import {DeviceService} from '../../service/device.service';

@UntilDestroy()
@Component({
  selector: 'pcp-action',
  templateUrl: './action.component.html',
  styleUrls: ['./action.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActionComponent {
  selected = new BehaviorSubject<Action | undefined>(undefined);
  options: Observable<any>;
  toUpdate = new Subject<any>();
  actionType = 'click';

  constructor(public actionsService: ActionsService,
              private http: HttpClient,
              route: ActivatedRoute,
              ds: DeviceService) {
    let device = '';
    let type = '';
    let number = '0';
    let profile = '';
    let idx = 0;
    this.options = combineLatest([combineAllParams(route), route.data]).pipe(untilDestroyed(this),
      tap(([ps, data]) => {
        device = ps.device;
        type = ps.type;
        number = ps.number;
        profile = ps.profile;
        this.actionType = data.type;

        idx = Number(number) - 1;
        if (type === 'slider') idx += 4;
      }),
      switchMap(() => combineLatest([ds.device$(device), this.actionType === 'click' ? actionsService.click : actionsService.analog])),
      map(([device, aa]) => {
        const config = device.activeProfile.actionsConfig;
        return [(this.actionType === 'click' ? config.knobActions : config.analogActions)[idx], aa];
      }),
      tap(([a, as]) => this.selected.next(as.find((aa: Action) => aa.impl === a.actionClass))),
      map(([a]) => a),
      shareReplay(1));
    this.options.subscribe();

    this.toUpdate.pipe(untilDestroyed(this), debounceTime(500), switchMap(val => this.save(device, type, number, val))).subscribe();
  }

  private save(device: string, control: string, idx: string, params: any): Observable<any> {
    const type = this.actionType === 'click' ? 'changeknob' : 'changeanalog';
    return this.http.post(`api/${type}`, {device, control, idx, params});
  }
}
