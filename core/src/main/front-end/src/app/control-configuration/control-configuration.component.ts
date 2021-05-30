import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

interface State {
  hasButton: boolean;
  hasAnalog: boolean;
  hasLabelLight: boolean;
}

@Component({
  selector: 'pcp-control-configuration',
  templateUrl: './control-configuration.component.html',
  styleUrls: ['./control-configuration.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ControlConfigurationComponent {
  state$: Observable<State>;

  constructor(private route: ActivatedRoute) {
    this.state$ = route.paramMap.pipe(map(ps => {
      const type = String(ps.get('type'));
      const hasButton = ['knob'].indexOf(type) !== -1;
      const hasAnalog = ['knob', 'slider'].indexOf(type) !== -1;
      const hasLabelLight = ['slider'].indexOf(type) !== -1;
      return {hasButton, hasAnalog, hasLabelLight};
    }));
  }
}
