import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'pcp-analog',
  templateUrl: './analog.component.html',
  styleUrls: ['./analog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AnalogComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
