import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'pcp-clickable',
  templateUrl: './clickable.component.html',
  styleUrls: ['./clickable.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ClickableComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
