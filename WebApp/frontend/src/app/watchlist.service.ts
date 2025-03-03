// watchlist.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
// import { Observable } from 'rxjs';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class WatchlistService {
  watchlistStatus: { [symbol: string]: boolean } = {};

  constructor(private http: HttpClient) {}

  getWatchlistData(): Observable<any[]> {
    return this.http.get<any[]>(`http://localhost:3000/watchlist`);
    // return this.http.get<any[]>(`/watchlist`);
  }

  addToWatchlist(symbol: string, companyName: string, lastPrice: number, change: number, percentChange: number): void {
    const companyDetails = { symbol, companyName, lastPrice, change, percentChange };
    this.watchlistStatus[symbol] = true;
    console.log(this.watchlistStatus);
    this.http.post<any>('http://localhost:3000/add-to-watchlist', companyDetails).subscribe({
      next: () => {
        console.log(`Added ${symbol} to watchlist`);
        console.log(this.watchlistStatus);
      },
      error: (error) => {
        console.error('Error adding to watchlist:', error);
      }
    });

    // this.http.post<any>('/add-to-watchlist', companyDetails).subscribe({
    //   next: () => {
    //     console.log(`Added ${symbol} to watchlist`);
    //     console.log(this.watchlistStatus);
    //   },
    //   error: (error) => {
    //     console.error('Error adding to watchlist:', error);
    //   }
    // });
  }

  removeFromWatchlist(symbol: string): void {
    const body = { symbol };
    this.watchlistStatus[symbol] = false;
    console.log(this.watchlistStatus);
    console.log('Remove Watch: ', body);
    this.http.post<any>('http://localhost:3000/remove-from-watchlist', body).subscribe({
      next: () => {
        console.log(`Removed ${symbol} from watchlist`);
      },
      error: (error) => {
        console.error('Error removing from watchlist:', error);
      }
    });

    // this.http.post<any>('/remove-from-watchlist', body).subscribe({
    //   next: () => {
    //     console.log(`Removed ${symbol} from watchlist`);
    //   },
    //   error: (error) => {
    //     console.error('Error removing from watchlist:', error);
    //   }
    // });
  }

  isInWatchlist(symbol: string): boolean {
    return this.watchlistStatus[symbol] || false; // Return false if the symbol is not found in the dictionary
  }
}
