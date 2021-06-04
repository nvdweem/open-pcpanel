import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {Action} from '../service/actions.service';
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
  private _action!: Action;
  group!: FormGroup;

  constructor() {
  }

  private buildControl(): void {
    const controls: { [key: string]: AbstractControl; } = {};

    controls.__type = new FormControl(this._action.impl);
    for (let ce of this._action.elements) {
      const name = (ce as any).name;
      if (name) {
        controls[name] = new FormControl();
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
}