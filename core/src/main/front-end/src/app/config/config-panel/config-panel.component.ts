import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output, TemplateRef} from '@angular/core';
import {Action, ConfigElement} from '../../service/actions.service';
import {AbstractControl, FormControl, FormGroup} from '@angular/forms';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {map, shareReplay, startWith, takeUntil} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {Color, ColorPickerControl} from '@iplab/ngx-color-picker';
import {MatDialog} from '@angular/material/dialog';
import {ColorString} from '@iplab/ngx-color-picker/lib/helpers/color.class';

export interface ListOption {
  value: string;
  display: string;
}

@UntilDestroy()
@Component({
  selector: 'pcp-config-panel',
  templateUrl: './config-panel.component.html',
  styleUrls: ['./config-panel.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfigPanelComponent {
  @Output() configChanged = new EventEmitter<any>();
  private sub?: Subscription;
  private _action!: Action;
  group!: FormGroup;
  listOptions: { [key: string]: Observable<ListOption[]> } = {};
  colorControls: { [key: string]: ColorPickerControl } = {};

  constructor(private http: HttpClient,
              private dialog: MatDialog) {
  }

  private buildControl(): void {
    const controls: { [key: string]: AbstractControl; } = {};

    controls.__type = new FormControl(this._action.impl);
    for (let ce of this._action.elements) {
      if (ce.name) {
        controls[ce.name] = new FormControl((ce as any).def);

        switch (ce.type) {
          case 'list':
            this.initList(ce, controls[ce.name]);
            break;
          case 'color':
            this.colorControls[ce.name] = new ColorPickerControl().hidePresets().hideAlphaChannel();
            break;
          case 'picklist':
            this.initPickList(ce, controls[ce.name]);
        }
      }
    }
    this.group = new FormGroup(controls);

    this.sub?.unsubscribe();
    this.sub = this.group.valueChanges.pipe(startWith(this.group.value), untilDestroyed(this)).subscribe(val => this.configChanged.next(val));
  }

  @Input()
  set action(a: Action) {
    this._action = a;
    this.buildControl();
  }

  @Input()
  set value(v: any) {
    this.group.patchValue(v);
  }

  get action(): Action {
    return this._action;
  }

  patch(name: string, value: number | null): void {
    const p: { [key: string]: any } = {};
    p[name] = value;
    this.group.patchValue(p);
  }

  private initList(ce: ConfigElement, control: AbstractControl): void {
    const allOptions = this.http.get<ListOption[]>(`api/${ce.listSource}`).pipe(shareReplay(1));
    this.listOptions[ce.name] = combineLatest([allOptions, control.valueChanges.pipe(startWith(control.value || ''))]).pipe(map(([vs, f]) => vs.filter(v => v.display.toLowerCase().indexOf(f) !== -1)));
  }

  listDisplayFn(name: string): (id: string) => string {
    return id => {
      let option: ListOption[] = [];
      this.listOptions[name].subscribe(vs => option = vs.filter(v => v.value === id)).unsubscribe();
      if (option.length === 1) {
        return option[0].display;
      }
      return id;
    };
  }

  showDialog(filepicker: TemplateRef<any>, controlName: string): void {
    const fp = this.dialog.open(filepicker);
    this.group.get(controlName)?.valueChanges.pipe(takeUntil(fp.afterClosed())).subscribe(() => fp.close());
  }

  setColor(control: AbstractControl, color: ColorString): void {
    control.setValue(Color.from(color).toRgbString());
  }

  getColor(control: AbstractControl): any {
    return Color.from(control.value);
  }

  private initPickList(ce: ConfigElement, control: AbstractControl): void {
    const allOptions = this.http.get<ListOption[]>(`api/${ce.listSource}`).pipe(shareReplay(1));
    const selected = control.valueChanges.pipe(startWith(control.value), map(() => {
      const vs = (control.value || []) as ListOption[];
      const rs: { [key: string]: string } = {};
      (vs || []).forEach(v => rs[v.value] = v.display);
      return rs;
    }));
    this.listOptions[ce.name] = combineLatest([allOptions, selected]).pipe(map(([vs, f]) => vs.filter(v => !f[v.value])));
  }

  addToPickList(control: AbstractControl, profile: ListOption): void {
    const ctrl = control as FormControl;
    const target = ctrl.value || [];
    target.push(profile);
    ctrl.setValue(target);
  }

  removeFromPickList(control: AbstractControl, profile: ListOption): void {
    const ctrl = control as FormControl;
    const target = (ctrl.value || []) as ListOption[];
    target.splice(target.findIndex(i => i.value === profile.value), 1);
    ctrl.setValue(target);
  }
}
