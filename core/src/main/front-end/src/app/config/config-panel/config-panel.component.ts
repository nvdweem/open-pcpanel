import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {Action, ConfigElement} from '../../service/actions.service';
import {AbstractControl, FormControl, FormGroup} from '@angular/forms';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {map, shareReplay, startWith} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';

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

  constructor(private http: HttpClient) {
  }

  private buildControl(): void {
    const controls: { [key: string]: AbstractControl; } = {};

    controls.__type = new FormControl(this._action.impl);
    for (let ce of this._action.elements) {
      if (ce.name) {
        controls[ce.name] = new FormControl((ce as any).def);
        if (ce.type === 'list') {
          this.initList(ce, controls[ce.name]);
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
    const filtered = combineLatest([allOptions, control.valueChanges.pipe(startWith(control.value || ''))]).pipe(map(([vs, f]) => vs.filter(v => v.display.toLowerCase().indexOf(f) !== -1)));
    this.listOptions[ce.name] = filtered;
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
}
