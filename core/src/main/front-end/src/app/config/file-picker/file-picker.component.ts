import {ChangeDetectionStrategy, Component, Input, Optional} from '@angular/core';
import {ControlContainer, FormControl} from '@angular/forms';
import {BehaviorSubject, Observable, ReplaySubject} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {shareReplay, switchMap, tap} from 'rxjs/operators';

interface File {
  name: string;
  fullPath: string;
  folder: boolean;
}

@Component({
  selector: 'pcp-file-picker',
  templateUrl: './file-picker.component.html',
  styleUrls: ['./file-picker.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FilePickerComponent {
  private control!: FormControl;
  loader = new BehaviorSubject(false);
  folder = new ReplaySubject<string>(1);
  files: Observable<File[]>;

  constructor(@Optional() private controlContainer: ControlContainer,
              http: HttpClient) {
    this.files = this.folder.pipe(
      tap(() => this.loader.next(true)),
      switchMap(path => http.get<File[]>(`api/files`, {params: {path}})),
      tap(() => this.loader.next(false)),
      shareReplay(1),
    );
  }

  @Input()
  set controlName(name: string) {
    this.control = this.controlContainer.control?.get(name) as FormControl;
    this.folder.next(this.control.value);
  }

  choose(file: File): void {
    if (file.folder) {
      this.folder.next(file.fullPath);
    } else {
      this.control.setValue(file.fullPath);
    }
  }

  toParent(folder: string): void {
    if (folder.endsWith('/') || folder.endsWith('\\')) {
      folder = folder.substr(0, folder.length - 1);
    }
    const length = Math.max(folder.lastIndexOf('/'), folder.lastIndexOf('\\')) + 1;
    this.folder.next(folder.substr(0, length));
  }
}
