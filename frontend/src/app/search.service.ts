import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class SearchService {

  constructor(private router: Router) { }

  navigateToSearch(symbol: string): void {
    console.log('Nothing is happening');
  }
}