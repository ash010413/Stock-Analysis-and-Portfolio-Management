import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class IntervalService {
  intervalID: any; // You can specify the correct type if needed

  constructor() {}

  setIntervalID(id: any): void {
    this.intervalID = id;
  }

  clearIntervalID(): void {
    clearInterval(this.intervalID);
  }
}
