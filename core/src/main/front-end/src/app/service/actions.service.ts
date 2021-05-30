import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, shareReplay} from 'rxjs/operators';
import {Observable} from 'rxjs';

export interface AllActions {
  analogActions: AnalogAction[];
}

export interface AnalogAction {
  name: string;
  configElements: ConfigElement[];
  impl: string;
}

export interface ConfigElement {
  name: string;
  label: string;
  type: 'textField' | 'textArea' | 'slider' | 'filePicker';
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
