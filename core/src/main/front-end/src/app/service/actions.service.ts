import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, shareReplay} from 'rxjs/operators';
import {Observable} from 'rxjs';

export interface AllActions {
  analogActions: Action[];
  knobActions: Action[];
}

export interface Action {
  name: string;
  elements: ConfigElement[];
  impl: string;
}

export interface ConfigElement {
  name: string;
  type: string;

  [key: string]: any;
}

@Injectable({
  providedIn: 'root'
})
export class ActionsService {
  actions: Observable<AllActions>;
  click: Observable<Action[]>;
  analog: Observable<Action[]>;

  constructor(http: HttpClient) {
    this.actions = http.get<AllActions>('api/actions').pipe(shareReplay(1));
    this.click = this.actions.pipe(map(aa => aa.knobActions));
    this.analog = this.actions.pipe(map(aa => aa.analogActions));
  }
}
