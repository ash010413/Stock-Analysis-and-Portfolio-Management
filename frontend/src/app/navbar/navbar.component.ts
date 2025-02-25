// import { Component } from '@angular/core';

// @Component({
//   selector: 'app-navbar',
//   templateUrl: './navbar.component.html',
//   styleUrls: ['./navbar.component.css']
// })
// export class NavbarComponent {

// }


import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { SearchResultsService } from '../search-results.service';


@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  isCollapsed: boolean = false;
  // @Input() symbol: any[];
  symbol: string = '';

  constructor(private router: Router, private searchResultsService: SearchResultsService) {}

  generateRouterLink(): string {
      // Check if there are existing search results
      const storedResults = this.searchResultsService.getStoredSearchResults();
      // console.log(storedResults)
      // console.log('generating router link');
      if (storedResults && storedResults.length > 0) {
        // console.log('Displaying from Stored: ', storedResults[0].symbol);
        return `/search/${storedResults[0].symbol}`;
      } else {
        return '/search/home';
      }
    }
  }  