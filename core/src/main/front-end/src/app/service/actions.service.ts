import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, shareReplay} from 'rxjs/operators';
import {Observable} from 'rxjs';

export interface AllActions {
  analogActions: AnalogAction[];
}

export interface AnalogAction {
  name: string;
  elements: ConfigElement[];
  impl: string;
}

export interface ConfigElement {
  [key: string]: any;
}

@Injectable({
  providedIn: 'root'
})
export class ActionsService {
  actions: Observable<AllActions>;
  analog: Observable<AnalogAction[]>;

  constructor(http: HttpClient) {
    this.actions = http.get<AllActions>('api/actions').pipe(shareReplay(1));
    this.analog = this.actions.pipe(map(aa => aa.analogActions));
  }
}
