import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TimestampService {
  constructor() {}

  getCurrentTimestamp(): number {
    return Math.floor(Date.now() / 1000);
  }
}
