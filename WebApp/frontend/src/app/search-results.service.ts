import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SearchResultsService {
  private searchResults: any[] = [];

  constructor() { }

  storeSearchResults(results: any[]): void {
    this.searchResults = results;
  }

  getStoredSearchResults(): any[] {
    return this.searchResults;
  }
}
