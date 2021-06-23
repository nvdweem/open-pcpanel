import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

interface State {
  hasButton: boolean;
  hasAnalog: boolean;
  hasLabelLight: boolean;
  title: string;
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
      let title = this.determineTitle(type, Number(ps.get('number')));
      return {hasButton, hasAnalog, hasLabelLight, title};
    }));
  }

  private determineTitle(type: string, nr: number): string {
    switch (type) {
      case 'knob':
        return `Knob ${nr}`;
      case 'slider':
        return `Slider ${nr}`;
      case 'body':
        return 'Body';
      case 'logo':
        return 'Logo';
      default:
        return 'Unknown';
    }
  }
}
