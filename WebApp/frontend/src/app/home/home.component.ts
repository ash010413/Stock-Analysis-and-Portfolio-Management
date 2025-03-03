import { Component, OnInit, HostListener, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormControl, NumberValueAccessor } from '@angular/forms';
import { ActivatedRoute, Router, NavigationEnd} from '@angular/router';
import { SearchResultsService } from '../search-results.service';
import { Observable, forkJoin, of } from 'rxjs';
import { PortfolioService } from '../portfolio.service';
import { catchError, concatMap } from 'rxjs/operators';
import { NewsModalComponent } from '../news-modal/news-modal.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Inject } from '@angular/core';
import { CompanydataService } from '../companydata.service';
import * as Highcharts from 'highcharts/highstock';
import { Options, SeriesOptionsType } from 'highcharts';
import { SeriesLineOptions, SeriesWordcloudOptions } from 'highcharts';
import { HighchartsService } from '../highcharts.service';
import {ChartsTabHighChartsService} from '../chartstabhighcharts.service';
import { WatchlistService } from '../watchlist.service';
import { SearchService } from '../search.service';
import { BuyModalComponent } from '../buy-modal/buy-modal.component';
import { BuyModalHomeComponent } from '../buy-modal-home/buy-modal-home.component';
import { NgbAlert } from '@ng-bootstrap/ng-bootstrap';
import { PortfoliosellmodalComponent } from '../portfoliosellmodal/portfoliosellmodal.component';
import { IntervalService } from '../interval.service';
import { TimestampService } from '../timestamp.service';
import { BuybuttonclickedService } from '../buybuttonclicked.service';

interface Company {
  name?: string;
  symbol?: string;
  exchange?: string;
  logo?: string;
  lastPrice?: number;
  change?: number;
  percentChange?: number;
  dateTime?: Date;
  highPrice?: number;
  lowPrice?: number;
  openPrice?: number;
  prevClose?: number;
  ipo?: Date;
  industry?: string;
  webpage?: string;
  currentTime?: Date;
  news: {
    category: string;
    headline: string;
    id: number;
    image: string;
    related: string;
    source: string;
    publishedDate: string;
    summary: string;
    url: string;
  }[];
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})

export class HomeComponent implements OnInit {
  company: Company | null = null;
  searchResults: any[] = [];
  fetchingSuggestions: boolean = false;
  symbol: string = '';
  searchControl = new FormControl();
  suggestions: any[] = [];
  auto: any; 
  activeTab: string = 'summary';
  starClass: string = '';
  loadingData!: boolean;
  invalidSymbol : boolean = false;
  checkSell : boolean = false;
  refreshInterval = 5000;
  timeStamp: number = 0;
  mergedCompanyData: any = {};
  currentTime: any = '';

  @ViewChild('selfClosingAlert', { static: false }) selfClosingAlert!: NgbAlert;
  @ViewChild('invalidSymbolAlert', { static: false }) invalidSymbolAlert!: NgbAlert;

  constructor(public buyButtonclickedService: BuybuttonclickedService,private portfolioService: PortfolioService, public route: ActivatedRoute, private intervalService: IntervalService, private timestampService: TimestampService, private http: HttpClient, private router: Router, public searchResultsService: SearchResultsService, @Inject(NgbModal) private modalService: NgbModal, private companyDataService: CompanydataService, public highchartsService: HighchartsService, public charttabService:ChartsTabHighChartsService,private watchlistService: WatchlistService, private searchService: SearchService){}

  
  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    // Check if the target element is an input with the name 'symbol'
    if (
      event.key === 'Backspace' &&
      (event.target instanceof HTMLInputElement) &&
      (event.target.getAttribute('name') === 'symbol') &&  // Check the name attribute
      (event.target as HTMLInputElement).value.trim().length === 1
    ) {
      this.clear();
    }
  }

  checkforSell(symbol: string): boolean {
    let symbolExists = false;
    this.portfolioService.getPortfolioData().subscribe(data => {
      // Check if any portfolio item has the specified symbol
      
      console.log(data.some(item => item.symbol === symbol));
      symbolExists = data.some(item => item.symbol === symbol);
    });
      if (symbolExists) {
        console.log('Symbol exists in portfolio');
        return true;
      } else {
        console.log('Symbol does not exist in portfolio');
        return false;
      }
  }
  
  
  ngOnInit(): void {
    this.invalidSymbol = false;
    this.checkSell = false;
    this.currentTime = `${new Date().getFullYear()}-${(new Date().getMonth()+1).toString().padStart(2, '0')}-${new Date().getDate().toString().padStart(2, '0')} ${new Date().getHours().toString().padStart(2, '0')}:${new Date().getMinutes().toString().padStart(2, '0')}:${new Date().getSeconds().toString().padStart(2, '0')}`;
    // console.log(this.timestampService.getCurrentTimestamp());
    this.route.paramMap.subscribe(params => {
      this.symbol = params.get('symbol') || '';
      if (this.symbol.trim() !== '') {
        this.router.navigate(['/search', this.symbol]);
        console.log('SYMBOL in INIT: ', this.symbol);
        const storedResults = this.searchResultsService.getStoredSearchResults();
        console.log('In INIT:', storedResults);
        this.searchResults = storedResults;
      }
      else {
        // this.searchResults = [];
        this.router.navigate(['/search/home']);
      }
    });
  }

  allFunctionCalls(symbol: string, companyData?: any) : void {
  this.mergedCompanyData = {
    ...companyData,
    // lastPrice: 0,
    // change: 0,
    // percentChange: 0,
    // dateTime: '',
    // isMarketOpen: false,
    // highPrice: 0,
    // lowPrice:  0,
    // openPrice: 0,
    // prevClose: 0,
    // peers: [],
    currentTime: `${new Date().getFullYear()}-${(new Date().getMonth()+1).toString().padStart(2, '0')}-${new Date().getDate().toString().padStart(2, '0')} ${new Date().getHours().toString().padStart(2, '0')}:${new Date().getMinutes().toString().padStart(2, '0')}:${new Date().getSeconds().toString().padStart(2, '0')}`,
    // news: [],
    // sentiment: {},
    // recommendation: [],
    // earnings: [],
    // historical: []
  };

 this.fetchHourlyData(symbol).subscribe((hourlyData) => {
    console.log('Hourly Data from Charts call: ',hourlyData);
    this.mergedCompanyData.hourly = hourlyData;
    console.log('From Hourly: ', this.mergedCompanyData);
    const lineColor = this.mergedCompanyData.change > 0 ? 'green' : 'red';
    const timestamps: string[] = hourlyData.map((result: any) => new Date(result.t).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false }));
    const stockPrices: number[] = hourlyData.map((result: any) => result.c);
    const data = timestamps.map((timestamp: string, index: number) => [timestamp, stockPrices[index]]);
    const totalEntries = timestamps.length;
    const stepSize = Math.ceil(totalEntries / 10); // Adjust the divisor as needed
    // const lineColor = this.changeValue >= 0 ? 'green' : 'red';
    console.log('From Hourly Call: ', data);
    this.highchartsService.chartOptions = {
      chart: {
        type: 'line',
        backgroundColor: '#f0f0f0'
        // width: 600, // Adjust the width of the chart
      },
      scrollbar: {
        enabled: true
      },
      legend: {
        enabled: false
      },
      title: {
        text: `${this.symbol} Hourly Price Variation`
      },
      xAxis: {
        categories: timestamps, // Use formatted timestamps for x-axis
        tickInterval: stepSize, // Set the tick interval for xAxis
        labels: {
          formatter: function() {
            return this.value.toString(); // Explicitly convert value to string
          }
        },
        tickWidth: 2, // Adjust the width of tick marks
        tickLength: 10 // Adjust the length of tick marks
      },
      yAxis: {
        title: {
          text: null // Remove yAxis title
        },
        opposite: true
      },
      navigator: {
        enabled: true
      },
      series: [{
        name: '', // Set series name to null
        type: 'line',
        color: lineColor, // Set line color based on changeValue
        data: data, // Use the array of data points
        marker: {
          enabled: false // Disable markers on the data points
        }
      }]
    };
    // this.initChart(hourlyData);
    console.log('Merged company data with hourly data: ', this.mergedCompanyData);
  });

  this.fetchRecommendationData(symbol).subscribe((recommendationData) => {
    this.mergedCompanyData.recommendation = recommendationData;
    console.log('Merged company data with recommendation data: ', this.mergedCompanyData); 
  });

  this.fetchQuoteDetails(symbol).subscribe((QuoteData) => {
    this.mergedCompanyData.lastPrice = QuoteData.c;
    this.mergedCompanyData.change = QuoteData.d;
    this.mergedCompanyData.percentChange = parseFloat(QuoteData.dp.toFixed(2));
    this.mergedCompanyData.dateTime = new Date(QuoteData.t * 1000).toLocaleString();
    this.mergedCompanyData.isMarketOpen = Math.floor((new Date().getTime() - (QuoteData.t * 1000))/1000) < 60;
    this.mergedCompanyData.highPrice = QuoteData.h;
    this.mergedCompanyData.lowPrice = QuoteData.l;
    this.mergedCompanyData.openPrice = QuoteData.o;
    this.mergedCompanyData.prevClose = QuoteData.pc;

    // const currentTimestamp = this.timestampService.getCurrentTimestamp();
    // this.timeStamp = currentTimestamp;

    console.log('Merged company data with quote data: ', this.mergedCompanyData);
  });

  this.fetchEarningsData(symbol).subscribe((earning) => {
    this.mergedCompanyData.earnings = earning;
    console.log('Merged company data with earnings data: ', this.mergedCompanyData);
  });

  this.fetchHistoricalData(symbol).subscribe((historicalData) => {
    this.mergedCompanyData.historical = historicalData;
    console.log('Historical Data: ', historicalData);
    if (!historicalData) {
      console.error('Historical data results are not available.');
      return;
  }
  const ohlc: any[] = [];
  const volume: any[] = [];

  historicalData.forEach((result: any) => {
    const timestamp = result.t; // UNIX timestamp
    const open = result.o;
    const high = result.h;
    const low = result.l;
    const close = result.c;
    const vol = result.v;
    ohlc.push([timestamp, open, high, low, close]); // Add OHLC data
    volume.push([timestamp, vol]); // Add volume data
    // console.log('OHLC data: ', ohlc);
    // console.log('Volume: ', volume);
});

const groupingUnits: [string, number[] | null][] = [
  ['day', [1]],     // Group by days
  ['week', [1]],    // Group by weeks
  ['month', [1, 3, 6]],  // Group by months, showing every 1st, 3rd, and 6th month
  ['year', null]    // Group by years
];

this.charttabService.SMAandVolumeChartOptions = {
  chart:{
    // height: 500,
    // width: 1275,
    backgroundColor: '#f0f0f0',
  },
  accessibility: {
    enabled: false
  },
  scrollbar: {
    enabled: true
  },
  legend: {
    enabled: false
  },
  rangeSelector: {
    allButtonsEnabled: true,
      enabled: true,
      inputEnabled: true,
    buttons: [{
      type: 'month',
      count: 1,
      text: '1m'
  }, {
      type: 'month',
      count: 3,
      text: '3m'
  }, {
      type: 'month',
      count: 6,
      text: '6m'
  }, {
      type: 'ytd',
      text: 'YTD'
  }, {
      type: 'year',
      count: 1,
      text: '1y'
  }, {
      type: 'all',
      text: 'All'
  }],
      selected: 2
  },
  title: {
      text: `${this.symbol} Historical`
  },
  subtitle: {
      text: 'With SMA and Volume by Price technical indicators'
  },
  xAxis: {
    type: 'datetime',
    labels: {
      formatter: function() {
        const date = new Date(this.value);
        const day = date.getDate();
        const month = date.toLocaleString('default', { month: 'short' });
        return day + ' ' + month;
      }
    }
  },
  yAxis: [{
      startOnTick: false,
      endOnTick: false,
      labels: {
          align: 'right',
          x: -3
      },
      title: {
          text: 'OHLC'
      },
      height: '60%',
      lineWidth: 2,
      resize: {
          enabled: true
      },
      opposite: true
  }, {
      labels: {
          align: 'right',
          x: -3
      },
      title: {
          text: 'Volume'
      },
      top: '65%',
      height: '35%',
      offset: 0,
      lineWidth: 2,
      opposite: true
  }],
  tooltip: {
      split: true
  },
  plotOptions: {
      series: {
          dataGrouping: {
              units: groupingUnits
          }
      }
  },
  series: [
    {
      type: 'candlestick',
      name: `${this.symbol}`,
      id: `${this.symbol}`,
      zIndex: 2,
      data: ohlc
  }, 
  {
      type: 'column',
      name: 'Volume',
      id: 'volume',
      data: volume,
      yAxis: 1        
  }, {
      type: 'vbp',
      linkedTo: `${this.symbol}`,
      params: {
          volumeSeriesID: 'volume'
      },
      dataLabels: {
          enabled: false
      },
      zoneLines: {
          enabled: false
      }
  }, {
      type: 'sma',
      linkedTo: `${this.symbol}`,
      zIndex: 1,
      marker: {
          enabled: false
      }
  }],
  navigator:{
    enabled: true
  }
};
});


this.fetchSentimentData(symbol).subscribe((sentiData) => {
  this.mergedCompanyData.sentiment = sentiData.map((sentiment: any) => {
    return {
      symbol: sentiment.symbol,
      year: sentiment.year,
      month: sentiment.month,
      mspr: sentiment.mspr,
      change: sentiment.change
    }
  });
  const allMSPR = sentiData.reduce((acc: number, curr: any) => acc + curr.mspr, 0);
  const allChange = sentiData.reduce((acc: number, curr: any) => acc + curr.change, 0);
  const positiveMSPR = sentiData.filter((item: any) => item.mspr > 0).reduce((acc: number, curr: any) => acc + curr.mspr, 0);
  const negativeMSPR = sentiData.filter((item: any) => item.mspr < 0).reduce((acc: number, curr: any) => acc + curr.mspr, 0);
  const positiveChange = sentiData.filter((item: any) => item.change > 0).reduce((acc: number, curr: any) => acc + curr.change, 0);
  const negativeChange = sentiData.filter((item: any) => item.change < 0).reduce((acc: number, curr: any) => acc + curr.change, 0);

  this.mergedCompanyData.allMSPR = allMSPR;
  this.mergedCompanyData.allChange = allChange;
  this.mergedCompanyData.positiveMSPR = positiveMSPR;
  this.mergedCompanyData.negativeMSPR = negativeMSPR;
  this.mergedCompanyData.positiveChange = positiveChange;
  this.mergedCompanyData.negativeChange = negativeChange;
  console.log('Merged company data with sentiment data: ', this.mergedCompanyData); 
});

this.fetchPeersData(symbol).subscribe((peersData) => {
  // Update the peers array in mergedCompanyData
  const filteredPeersData = peersData.filter(peer => !peer.includes('.'));
  this.mergedCompanyData.peers = filteredPeersData;
  console.log('Merged company data with peers:', this.mergedCompanyData);
  
  this.company = this.mergedCompanyData;
  // this.searchResults.push(mergedCompanyData);
  // this.searchResultsService.storeSearchResults([mergedCompanyData]);
  console.log('Merged company data:', this.mergedCompanyData);
  // this.searchResults.push(mergedCompanyData);
  // this.searchResultsService.storeSearchResults([mergedCompanyData]);
  // this.router.navigate(['/search', this.symbol]);
});


this.fetchNewsData(symbol).subscribe((newsData) => {
  // mergedCompanyData.news = newsData;
  
  this.mergedCompanyData.news = newsData.filter((newsItem: any) => newsItem.image !== '').map((newsItem: any) => {
    return {
      category: newsItem.category,
      headline: newsItem.headline,
      id: newsItem.id,
      image: newsItem.image,
      related: newsItem.related,
      source: newsItem.source,
      publishedDate: newsItem.datetime * 1000,
      summary: newsItem.summary,
      url: newsItem.url
    };
  }).slice(0,20);
  console.log('Merged company data with news data: ', this.mergedCompanyData);
});

this.company = this.mergedCompanyData;

this.searchResults.push(this.mergedCompanyData);
this.searchResultsService.storeSearchResults([this.mergedCompanyData]);
this.companyDataService.setMergedCompanyData(this.mergedCompanyData);
this.startRefreshInterval();
this.router.navigate(['/search', this.symbol]);
}

fetchQuoteDetails(symbol: string): Observable<any> {
  return this.http.get<any>(`http://localhost:3000/quoteDetails?symbol=${symbol}`);
}

// fetchQuoteDetails(symbol: string): Observable<any> {
//   return this.http.get<any>(`/quoteDetails?symbol=${symbol}`);
// }

search(): void {
  this.company = null;
  this.suggestions = [];
  this.searchResults = [];
  this.loadingData = true;
  console.log('In HOME SEARCH');
  if (this.symbol.trim() === '') {
    this.invalidSymbol = true;
    console.error('Symbol is required');
    this.loadingData = false;
    return;
  }
  console.log('SYMBOL in home: ',this.symbol);

  this.http.post<any>('http://localhost:3000/search', { symbol: this.symbol }).subscribe({
    next: (data) => {
      console.log('Stock data directly from Backend:', data);
      const companyData = {
        name: data.name,
        symbol: data.ticker,
        webpage: data.weburl,
        exchange: data.exchange,
        logo: data.logo,
        ipo: data.ipo,
        industry: data.finnhubIndustry,
      };
      if (companyData.name == undefined) {
        this.invalidSymbol = true;
        this.loadingData = false;
        return
      }
      console.log('Before Function Calls: ', companyData);
      this.allFunctionCalls(this.symbol, companyData);
      this.loadingData = false;

    },
    error: (error) => {
      this.invalidSymbol = true;
      console.error('Error fetching stock data:', error);
      this.loadingData = false;
    }
  });

  // this.http.post<any>('/search', { symbol: this.symbol }).subscribe({
  //   next: (data) => {
  //     console.log('Stock data directly from Backend:', data);
  //     const companyData = {
  //       name: data.name,
  //       symbol: data.ticker,
  //       webpage: data.weburl,
  //       exchange: data.exchange,
  //       logo: data.logo,
  //       ipo: data.ipo,
  //       industry: data.finnhubIndustry,
  //     };
  //     if (companyData.name == undefined) {
  //       this.invalidSymbol = true;
  //       this.loadingData = false;
  //       return
  //     }
  //     console.log('Before Function Calls: ', companyData);
  //     this.allFunctionCalls(this.symbol, companyData);
  //     this.loadingData = false;

  //   },
  //   error: (error) => {
  //     this.invalidSymbol = true;
  //     console.error('Error fetching stock data:', error);
  //     this.loadingData = false;
  //   }
  // });

  // this.router.navigate(['/search', this.symbol]);
}

activateTab(tab: string): void {
  this.activeTab = tab;
}

startRefreshInterval() : void{
  this.intervalService.setIntervalID(setInterval(() => {
    this.fetchQuoteDetails(this.symbol);
    this.fetchQuoteDetails(this.symbol).subscribe((QuoteData) => {
      console.log('QuoteData: ', QuoteData);
      this.mergedCompanyData.change = QuoteData.d;
      this.mergedCompanyData.lastPrice = QuoteData.c;
      this.mergedCompanyData.percentChange = parseFloat(QuoteData.dp.toFixed(2));
      this.mergedCompanyData.currentTime = `${new Date().getFullYear()}-${(new Date().getMonth()+1).toString().padStart(2, '0')}-${new Date().getDate().toString().padStart(2, '0')} ${new Date().getHours().toString().padStart(2, '0')}:${new Date().getMinutes().toString().padStart(2, '0')}:${new Date().getSeconds().toString().padStart(2, '0')}`
      console.log('Timestamp: ', this.mergedCompanyData.currentTime); 
    })
    console.log('Refresh!!!');
  }, this.refreshInterval));
}

openModal(newsItem: any) {
  const modalRef = this.modalService.open(NewsModalComponent);
  modalRef.componentInstance.source = newsItem.source;
  modalRef.componentInstance.publishedDate = newsItem.publishedDate;
  modalRef.componentInstance.headline = newsItem.headline;
  modalRef.componentInstance.summary = newsItem.summary;
  modalRef.componentInstance.url = newsItem.url;
}

openBuyModal(company: any) {
  console.log(company);
  const buyModalRef = this.modalService.open(BuyModalComponent);
  buyModalRef.componentInstance.symbol = company.symbol;
  buyModalRef.componentInstance.currentPrice = company.lastPrice;
  buyModalRef.componentInstance.companyName = company.name;

  buyModalRef.closed.subscribe((result: any) => {
    if (this.buyButtonclickedService.buyButtonClicked == true){
      
    }
  })
  
  this.http.get<any>('http://localhost:3000/balance').subscribe(data => {
    buyModalRef.componentInstance.balanceWallet = data?.balance || 0;
  })

  // this.http.get<any>('/balance').subscribe(data => {
  //   buyModalRef.componentInstance.balanceWallet = data?.balance || 0;
  // })
}

openSellModal(company: any) {
  console.log(company);
  const sellModalRef = this.modalService.open(PortfoliosellmodalComponent);
  sellModalRef.componentInstance.symbol = company.symbol;
  sellModalRef.componentInstance.currentPrice = company.lastPrice;
  sellModalRef.componentInstance.companyName = company.name;

  this.http.get<any>('http://localhost:3000/balance').subscribe({
      next: (data) => {
        console.log(data);
        // if (!sellModalRef.componentInstance.balanceWallet) {
        //   sellModalRef.componentInstance.balanceWallet = {};
        // }
        sellModalRef.componentInstance.balanceWallet  = data.balance;
      },
      error: (error) => {
        console.error('Error fetching balance:', error);
      }
    });

    // this.http.get<any>('/balance').subscribe({
    //   next: (data) => {
    //     console.log(data);
    //     // if (!sellModalRef.componentInstance.balanceWallet) {
    //     //   sellModalRef.componentInstance.balanceWallet = {};
    //     // }
    //     sellModalRef.componentInstance.balanceWallet  = data.balance;
    //   },
    //   error: (error) => {
    //     console.error('Error fetching balance:', error);
    //   }
    // });
  
  this.http.get<any[]>('http://localhost:3000/portfolio').subscribe({
    next: (data) => {
      console.log(data);
      sellModalRef.componentInstance.oldQuantity = data[0].quantity;
    }
  })

  // this.http.get<any[]>('/portfolio').subscribe({
  //   next: (data) => {
  //     console.log(data);
  //     sellModalRef.componentInstance.oldQuantity = data[0].quantity;
  //   }
  // })
}

isMarketOpen(dateTime: string): boolean {
  const marketOpenHour = 9;
  const marketCloseHour = 16;

  const hour = parseInt(dateTime.split(',')[1].split(':')[0]);

  return hour >= marketOpenHour && hour < marketCloseHour;
}

fetchSentimentData(symbol: string): Observable <[]> {
  const sentiUrl = `http://localhost:3000/insider-sentiment?q=${symbol}`;
  const sentiData = this.http.get<any>(sentiUrl);
  // console.log('Sentiment data from home component: ', sentiData);
  return sentiData
}


// fetchSentimentData(symbol: string): Observable <[]> {
//   const sentiUrl = `/insider-sentiment?q=${symbol}`;
//   const sentiData = this.http.get<any>(sentiUrl);
//   // console.log('Sentiment data from home component: ', sentiData);
//   return sentiData
// }

watchlistStatus: { [symbol: string]: boolean } = {};

successMessage: string | undefined;
dangerMessage: string | undefined;

showSuccessAlert(symbol: string): void {
  this.successMessage = `${symbol} is added to watchlist`;
  setTimeout(() => {
    this.successMessage = '';
  }, 5000); // 5000 milliseconds = 5 seconds, adjust as needed
}

showDangerAlert(symbol: string): void {
  this.dangerMessage = `${symbol} is removed from watchlist`;
  setTimeout(() => {
    this.dangerMessage = '';
  }, 5000); // 5000 milliseconds = 5 seconds, adjust as needed
}

addToWatchlist(symbol: string, companyName: string, lastPrice: number, change: number, percentChange: number): void {
  this.watchlistService.addToWatchlist(symbol, companyName, lastPrice, change, percentChange);
}

removeFromWatchlist(symbol: string): void {
  this.watchlistService.removeFromWatchlist(symbol);
}

isInWatchlist(symbol: string): boolean {
  return this.watchlistService.isInWatchlist(symbol);
}

fetchHourlyData(symbol: string) {
  const hourlyUrl = `http://localhost:3000/hourly?q=${symbol}`;
   return this.http.get<any>(hourlyUrl);
}

// fetchHourlyData(symbol: string) {
//   const hourlyUrl = `/hourly?q=${symbol}`;
//    return this.http.get<any>(hourlyUrl);
// }

fetchHistoricalData(symbol: string): Observable <[]> {
  const histUrl = `http://localhost:3000/historical?q=${symbol}`;
  return this.http.get<any>(histUrl);
}

// fetchHistoricalData(symbol: string): Observable <[]> {
//   const histUrl = `/historical?q=${symbol}`;
//   return this.http.get<any>(histUrl);
// }

// fetchEarningsData(symbol: string): Observable <[]> {
//   const earningsUrl = `/earnings?q=${symbol}`;
//   return this.http.get<any>(earningsUrl);
// }

fetchEarningsData(symbol: string): Observable <[]> {
  const earningsUrl = `http://localhost:3000/earnings?q=${symbol}`;
  return this.http.get<any>(earningsUrl);
}

fetchRecommendationData(symbol: string): Observable <[]> {
  const recommendationUrl = `http://localhost:3000/recommendation?q=${symbol}`;
  return this.http.get<any>(recommendationUrl);
}

// fetchRecommendationData(symbol: string): Observable <[]> {
//   const recommendationUrl = `/recommendation?q=${symbol}`;
//   return this.http.get<any>(recommendationUrl);
// }

fetchNewsData(symbol: string): Observable<[]> {
  const newsUrl =  `http://localhost:3000/news?q=${symbol}`;
  return this.http.get<any>(newsUrl);
}

// fetchNewsData(symbol: string): Observable<[]> {
//   const newsUrl =  `/news?q=${symbol}`;
//   return this.http.get<any>(newsUrl);
// }

fetchPeersData(symbol: string): Observable<string[]> {
  const peersUrl = `http://localhost:3000/peers?q=${symbol}`;
  return this.http.get<string[]>(peersUrl);
}

// fetchPeersData(symbol: string): Observable<string[]> {
//   const peersUrl = `/peers?q=${symbol}`;
//   return this.http.get<string[]>(peersUrl);
// }

searchPeer(event: MouseEvent, peer: string): void {
  clearInterval(this.intervalService.intervalID);
  event.preventDefault();
  console.log('Searching for peer:', peer);
  this.company = null;
  this.searchResults = [];
  this.symbol = peer;
  this.searchResultsService.storeSearchResults([]);
  // Make HTTP request to backend server to get peer data
  // Adjust the URL and request parameters as needed
  this.http.post<any>('http://localhost:3000/search', { symbol: peer }).subscribe({
    next: (data) => {
      console.log('Peer data:', data);
      const companyData = {
        name: data.name,
        symbol: data.ticker,
        exchange: data.exchange,
        logo: data.logo,
        ipo: data.ipo,
        industry: data.finnhubIndustry,
        webpage: data.weburl
      };
      this.allFunctionCalls(peer, companyData);

      const storedResults = this.searchResultsService.getStoredSearchResults();
      console.log(storedResults);
      this.searchResults = storedResults;
     
      // Process the data and handle the response as needed
      // For example, you can navigate to the peer route here
      this.router.navigate(['/search', peer]);
        // console.log('Search: ', this.searchResults);
        // const hourlyData = storedResults[0].hourly;
        // console.log('Hourly: ',hourlyData);
        // this.initChart(this.searchResults[0].hourly);
        // this.router.navigate(['/search', this.symbol]);
    },
    error: (error) => {
      console.error('Error fetching peer data:', error);
    }
  });

  // this.http.post<any>('/search', { symbol: peer }).subscribe({
  //   next: (data) => {
  //     console.log('Peer data:', data);
  //     const companyData = {
  //       name: data.name,
  //       symbol: data.ticker,
  //       exchange: data.exchange,
  //       logo: data.logo,
  //       ipo: data.ipo,
  //       industry: data.finnhubIndustry,
  //       webpage: data.weburl
  //     };
  //     this.allFunctionCalls(peer, companyData);

  //     const storedResults = this.searchResultsService.getStoredSearchResults();
  //     console.log(storedResults);
  //     this.searchResults = storedResults;
     
  //     // Process the data and handle the response as needed
  //     // For example, you can navigate to the peer route here
  //     this.router.navigate(['/search', peer]);
  //       // console.log('Search: ', this.searchResults);
  //       // const hourlyData = storedResults[0].hourly;
  //       // console.log('Hourly: ',hourlyData);
  //       // this.initChart(this.searchResults[0].hourly);
  //       // this.router.navigate(['/search', this.symbol]);
  //   },
  //   error: (error) => {
  //     console.error('Error fetching peer data:', error);
  //   }
  // });
}

fetchSuggestions(): void {
  this.suggestions = [];
  this.fetchingSuggestions = true;
  if (this.symbol.trim() === '') {
    this.suggestions = []; // Clear suggestions if input is empty
    this.fetchingSuggestions = false;
    return;
  }
  const autocompleteUrl = `http://localhost:3000/autocomplete?q=${this.symbol}`
  // const autocompleteUrl = `/autocomplete?q=${this.symbol}`
  this.http.get<any>(autocompleteUrl).subscribe({
    next: (data) => {
      console.log('Suggested: ', data.result);

      this.suggestions = data.result.filter((item: any) => {

        return item.type === 'Common Stock' && !item.symbol.includes('.');
      }).map((item: any) => ({
        displaySymbol: item.displaySymbol,
        description: item.description
      }));
      // console.log('Suggestions:', this.suggestions);
    },
    error: (error) => {
      console.error('Error fetching suggestions:', error);
    },
    complete: () => {
      this.fetchingSuggestions = false;
    }
  });
}

selectSuggestion(suggestion: any): void {
  this.symbol = suggestion.displaySymbol;
  this.suggestions = [];
  this.search();
}

clear(): void {
  this.company = null;
  this.suggestions = [];
  this.searchResults = [];
  this.symbol = '';
  this.searchResultsService.storeSearchResults([]);
  clearInterval(this.intervalService.intervalID);
  this.intervalService.clearIntervalID();
  this.router.navigate(['/search/home']);
}

}






