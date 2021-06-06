import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {Device} from '../../service/device.service';
import {BehaviorSubject} from 'rxjs';

export interface ClickEvent {
  control: 'knob' | 'slider' | 'logo' | 'body';
  idx: number;
}

interface WebSocketMessage {
  type?: string;
  idx: number;
  value: number;
  color?: string;
  color2?: string;
  color3?: string;
  color4?: string;
  color5?: string;
}

@Component({
  selector: 'pcp-pro',
  templateUrl: './pro.component.html',
  styleUrls: ['./pro.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProComponent {
  @Output() controlClicked = new EventEmitter<any>();
  states = new BehaviorSubject([0, 0, 0, 0, 0, 0, 0, 0, 0]);
  colors = new BehaviorSubject(['#000', '#000', '#000', '#000', '#000', ['#000', '#000', '#000', '#000', '#000'], ['#000', '#000', '#000', '#000', '#000'], ['#000', '#000', '#000', '#000', '#000'], ['#000', '#000', '#000', '#000', '#000']]);
  sliderLabelColors = new BehaviorSubject(['#000', '#000', '#000', '#000']);
  logoColor = new BehaviorSubject('#000');

  @Input()
  set device(device: Device | null) {
    if (!device) return;

    this.states.next(device.states);
    this.connectSocket(device);
  }

  select(control: string, idx: number | string): void {
    this.controlClicked.next({control, idx: Number(idx)});
  }

  private connectSocket(device: Device): void {
    const ws = new WebSocket(`${window.location.origin.replace(/^http/, 'ws')}/api/deviceSocket`);
    ws.onopen = () => ws.send(device.id);
    ws.onmessage = (m) => {
      const msg = JSON.parse(m.data) as WebSocketMessage;

      if (!msg.type) {
        this.states.value[msg.idx] = msg.value;
        this.states.next(this.states.value);

        if (msg.color) {
          if (msg.idx < 4) {
            this.colors.value[msg.idx] = msg.color;
          } else {
            this.colors.value[msg.idx] = [msg.color, msg.color2!, msg.color3!, msg.color4!, msg.color5!];
          }
          this.colors.next(this.colors.value);
        }
      } else {
        switch (msg.type) {
          case 'slider-label':
            msg.color && (this.sliderLabelColors.value[msg.idx] = msg.color);
            this.sliderLabelColors.next(this.sliderLabelColors.value);
            break;
          case 'logo':
            if (msg.color) {
              this.logoColor.next(msg.color);
            }
            break;
        }
      }
    };
  }
}
