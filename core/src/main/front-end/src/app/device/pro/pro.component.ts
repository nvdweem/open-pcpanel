import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {Device} from '../../service/device.service';

export interface ClickEvent {
  control: 'knob' | 'slider' | 'logo' | 'body';
  idx: number;
}

@Component({
  selector: 'pcp-pro',
  templateUrl: './pro.component.html',
  styleUrls: ['./pro.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProComponent {
  @Output() controlClicked = new EventEmitter<any>();
  knob1 = 0;
  knob2 = 25;
  knob3 = 50;
  knob4 = 75;
  knob5 = 100;
  slider1 = 0;
  slider2 = 33;
  slider3 = 66;
  slider4 = 100;

  @Input()
  set device(device: Device | null) {
    if (!device) return;

    [this.knob1, this.knob2, this.knob3, this.knob4, this.knob5,
      this.slider1, this.slider2, this.slider3, this.slider4] = device.states;
  }

  select(control: string, idx: number | string): void {
    this.controlClicked.next({control, idx: Number(idx)});
  }
}
