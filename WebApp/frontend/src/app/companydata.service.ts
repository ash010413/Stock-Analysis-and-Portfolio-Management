import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CompanydataService {

  private mergedCompanyDataSubject = new BehaviorSubject<any>(null);
  mergedCompanyData$ = this.mergedCompanyDataSubject.asObservable();

  constructor() { }

  setMergedCompanyData(data: any) {
    this.mergedCompanyDataSubject.next(data);
  }
}