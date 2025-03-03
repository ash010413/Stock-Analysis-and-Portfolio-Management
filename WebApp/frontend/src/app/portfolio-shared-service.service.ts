import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PortfolioSharedService {
  private buyOrNotSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public buyOrNot$ = this.buyOrNotSubject.asObservable();

  constructor() {}

  setBuyOrNot(value: boolean) {
    this.buyOrNotSubject.next(value);
  }

  getBuyOrNotValue(): boolean {
    return this.buyOrNotSubject.getValue();
  }
}
