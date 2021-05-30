import {ChangeDetectionStrategy, ChangeDetectorRef, Component} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {debounceTime, map, take} from 'rxjs/operators';
import {combineLatest, Observable} from 'rxjs';
import {FormControl, FormGroup} from '@angular/forms';
import {Color, ColorPickerControl} from '@iplab/ngx-color-picker';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {HttpClient} from '@angular/common/http';
import {combineAllParams} from '../../../helper';
import {DeviceService, Profile} from '../../service/device.service';

interface State {
  hasWave: boolean;
  hasStatic: boolean;
  hasVolumeGradient: boolean;
  hasGradient: boolean;
  hasRainbow: boolean;
  hasBreath: boolean;
}

type LightType = 'static' | 'wave' | 'rainbow' | 'breath' | 'volumeGradient' | 'gradient';

@UntilDestroy()
@Component({
  selector: 'pcp-light',
  templateUrl: './light.component.html',
  styleUrls: ['./light.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LightComponent {
  state$: Observable<State>;
  group: FormGroup;
  regular1 = new ColorPickerControl().hidePresets().hideAlphaChannel();
  regular2 = new ColorPickerControl().hidePresets().hideAlphaChannel();

  constructor(private http: HttpClient,
              private cdr: ChangeDetectorRef,
              deviceService: DeviceService,
              route: ActivatedRoute) {
    this.group = new FormGroup({
      device: new FormControl(),
      control: new FormControl(),
      idx: new FormControl(),
      type: new FormControl('static'),
      color1: new FormControl(),
      color2: new FormControl(),
      brightness: new FormControl(255),
      speed: new FormControl(100),
      phaseShift: new FormControl(100),
      reverse: new FormControl(),
      bounce: new FormControl(),
    });

    this.state$ = combineLatest([combineAllParams(route), route.data]).pipe(map(([ps, data]) => {
      const type = data.label ? 'slider-label' : ps.type;
      this.group.patchValue({
        device: ps.device,
        control: type,
        idx: ps.number,
      });

      deviceService.devices$.pipe(take(1)).subscribe(ds => {
        const profile = ds.find(d => d.id === ps.device)?.profiles?.find(p => p.id === Number(ps.profile));
        if (!profile) return;

        this.getConfig(profile, type, Number(ps.number));
      });

      return {
        hasWave: ['body'].indexOf(type) !== -1,
        hasRainbow: ['body', 'logo'].indexOf(type) !== -1,
        hasBreath: ['body', 'logo'].indexOf(type) !== -1,
        hasStatic: true,
        hasVolumeGradient: ['knob', 'slider'].indexOf(type) !== -1,
        hasGradient: ['slider'].indexOf(type) !== -1,
      } as State;
    }));
    this.group.valueChanges.pipe(untilDestroyed(this), debounceTime(100)).subscribe(vs => this.update(vs));
  }

  private update(vs: any): void {
    this.http.post('api/changelight', vs).subscribe();
  }

  get selected(): LightType {
    return this.group.get('type')?.value as LightType;
  }

  set selected(type: LightType) {
    this.group.patchValue({type});
  }

  get color1(): string {
    return this.group.get('color1')?.value || '#FFF';
  }

  set color1(color1: string) {
    this.group.patchValue({color1});
  }

  get color2(): string {
    return this.group.get('color2')?.value || '#FFF';
  }

  set color2(color2: string) {
    this.group.patchValue({color2});
  }

  get color(): Color {
    return Color.from(this.color1);
  }

  set color(col: Color) {
    this.color1 = col.toRgbString();
  }

  get clr(): Color {
    return Color.from('#FFF');
  }

  set clr(_: Color) {
    // Don't care
  }

  private getConfig(profile: Profile, type: string, number: number): void {
// @formatter:off
    let key = '';
    switch (type) {
      case 'knob': key = 'knobs'; break;
      case 'slider': key = 'sliders'; break;
      case 'slider-label': key = 'sliderLabels'; break;
      case 'logo': key = 'logo'; break;
      case 'body': key =  '';
    }
// @formatter:on

    const cfg = profile.lightConfig?.[key];
    const patching = (number === 0 ? cfg : cfg?.[number - 1]) || {};

    if (patching.type === 'gradient' && this.group.value.control === 'knob') {
      patching.type = 'volumeGradient';
    }
    if (!patching.type || patching.type === 'empty') {
      patching.type = 'static';
    }
    this.group.patchValue(patching);
    this.cdr.markForCheck(); // The chrome-picker doesn't play nice with OnPush
  }
}
