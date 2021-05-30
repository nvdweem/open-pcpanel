import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {AnalogAction} from '../service/actions.service';
import {AbstractControl, FormControl, FormGroup} from '@angular/forms';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {Subscription} from 'rxjs';
import {startWith} from 'rxjs/operators';

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
  private _action!: AnalogAction;
  group!: FormGroup;

  constructor() {
  }

  private buildControl(): void {
    const controls: { [key: string]: AbstractControl; } = {};
    controls.__type = new FormControl(this._action.impl);
    for (let ce of this._action.configElements) {
      controls[ce.name] = new FormControl();
    }
    this.group = new FormGroup(controls);

    this.sub?.unsubscribe();
    this.sub = this.group.valueChanges.pipe(startWith(this.group.value), untilDestroyed(this)).subscribe(val => this.configChanged.next(val));
  }

  @Input()
  set action(a: AnalogAction) {
    this._action = a;
    this.buildControl();
  }

  get action(): AnalogAction {
    return this._action;
  }

  patch(name: string, value: number | null): void {
    const p: { [key: string]: any } = {};
    p[name] = value;
    this.group.patchValue(p);
  }
}
