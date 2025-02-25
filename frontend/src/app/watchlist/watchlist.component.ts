import { Component, OnInit } from '@angular/core';
import { WatchlistService } from '../watchlist.service';
import { HttpClient } from '@angular/common/http';
import { HomeComponent } from '../home/home.component';
import { Observable } from 'rxjs';
import { IntervalService } from '../interval.service';

@Component({
  selector: 'app-watchlist',
  templateUrl: './watchlist.component.html',
  styleUrls: ['./watchlist.component.css']
})

export class WatchlistComponent implements OnInit {
  watchlistData: any[] = [];
  loadingWatchlist: boolean = true;
  // companyName: any = this.homeComponent.company?.name;
  quoteData : any[] = [];
  // QuoteData !: any;

  constructor(private intervalService: IntervalService, private http: HttpClient, private watchlistService: WatchlistService, private homeComponent: HomeComponent){}

  ngOnInit(): void {
    // console.log('Watchlist INIT');
    // const symbols = Object.keys(this.watchlistService.watchlistStatus).filter(symbol => this.watchlistService.watchlistStatus[symbol]);
    // console.log('WatchlistStatus Symbols in INIT: ', symbols);
    // console.log('fetching quote and updating');
    // this.watchlistService.getWatchlistData().subscribe(data => {
    //   console.log(data);
    //   console.log('WatchlistStatus: ',this.watchlistService.watchlistStatus);
    //   const symbols = Object.keys(this.watchlistService.watchlistStatus).filter(symbol => this.watchlistService.watchlistStatus[symbol]);
    //   console.log(symbols);
    //   const symbolDataMap: { [symbol: string]: any } = {};
    //   data.forEach(item => {
    //     // Check if the symbol is present in the symbols array
    //     if (symbols.includes(item.symbol)) {
    //       // If the symbol is present, update the dictionary with the latest data
    //       symbolDataMap[item.symbol] = item;
    //     }
    //   });
    //   // console.log(symbolDataMap);
    //   const filteredData = Object.values(symbolDataMap);
    //   this.watchlistData = filteredData;
    //   console.log('Retrieved Data: ', filteredData);
    //   filteredData.forEach(item => {
    //   console.log('Before Quote: ', item);
    //   this.fetchQuoteDetails(item.symbol).subscribe((QuoteData) => {
    //     console.log('Inside Function Call: ', QuoteData);

    //     const quoteDetails = {
    //       symbol: item.symbol,
    //       companyName: item.companyName,
    //       currentPrice: QuoteData.c,
    //       change: QuoteData.d,
    //       percentChange: parseFloat(QuoteData.dp.toFixed(2))
    //     };

    //     this.quoteData.push(quoteDetails);
     
    //     console.log('Local Quote: ',this.quoteData);
    //       this.watchlistService.addToWatchlist(item.symbol,item.companyName,QuoteData.c,QuoteData.d,parseFloat(QuoteData.dp.toFixed(2)));
    //     })
    // })
    // })
    // this.consoleQuoteData();
    this.fetchWatchlistData();
  };

  consoleQuoteData() {
    console.log('INSIDE DUMMY');
    console.log(this.quoteData);
  }

  fetchQuoteDetails(symbol: string): Observable<any> {
    console.log('COMING FROM QUOTE');
    return this.http.get<any>(`http://localhost:3000/quoteDetails?symbol=${symbol}`);
    // return this.http.get<any>(`/quoteDetails?symbol=${symbol}`);
  }

  onCardClick(symbol: string): void {
    this.homeComponent.company = null;
    this.homeComponent.suggestions = [];
    this.homeComponent.searchResults = [];
    this.homeComponent.symbol = '';
    this.homeComponent.searchResultsService.storeSearchResults([]);
    clearInterval(this.intervalService.intervalID);
    console.log(symbol);
    this.homeComponent.symbol = symbol
    this.homeComponent.search();
  }

  onCloseButtonClick(event: MouseEvent, symbol: string) {
    event.stopPropagation();
    this.watchlistService.removeFromWatchlist(symbol);

    // this.http.get<any>(`http://localhost:3000/quoteDetails?symbol=${symbol}`)
    this.fetchWatchlistData();
  }

  fetchWatchlistData(): void {
    this.watchlistService.getWatchlistData().subscribe(data => {
      console.log(data);
      console.log('WatchlistStatus: ',this.watchlistService.watchlistStatus);
      const symbols = Object.keys(this.watchlistService.watchlistStatus).filter(symbol => this.watchlistService.watchlistStatus[symbol]);
      console.log(symbols);
      const symbolDataMap: { [symbol: string]: any } = {};
      data.forEach(item => {
        // Check if the symbol is present in the symbols array
        if (symbols.includes(item.symbol)) {
          // If the symbol is present, update the dictionary with the latest data
          symbolDataMap[item.symbol] = item;
        }
      });
      console.log(symbolDataMap);
      const filteredData = Object.values(symbolDataMap);
      this.watchlistData = filteredData;
      console.log(this.watchlistData);
      this.loadingWatchlist = false;
      return filteredData
    });
  }

}

