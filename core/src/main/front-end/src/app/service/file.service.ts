import {Injectable} from '@angular/core';
import {fromEvent, Observable, Subject} from 'rxjs';
import {filter, map, takeUntil} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class FileService {
  private stop$ = new Subject<any>();

  download(body: string, filename: string, type: string = 'text/plain'): void {
    const element = document.createElement('a');
    element.setAttribute('href', `data:${type};charset=utf-8,${encodeURIComponent(body)}`);
    element.setAttribute('download', filename);
    element.style.display = 'none';

    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  }

  upload(): Observable<FileList> {
    this.stop$.next();
    const element = document.createElement('input');
    element.setAttribute('type', 'file');
    element.style.display = 'none';
    element.click();
    return fromEvent(element, 'change').pipe(takeUntil(this.stop$), map(() => element.files), filter(fs => !!fs)) as Observable<FileList>;
  }
}
