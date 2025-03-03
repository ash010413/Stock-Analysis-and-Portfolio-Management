import { Component, OnInit, ViewChild } from '@angular/core';
import { PortfolioService } from '../portfolio.service';
import { HomeComponent } from '../home/home.component';
import { HttpClient } from '@angular/common/http';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Inject } from '@angular/core';
import { PortfoliobuymodalComponent } from '../portfoliobuymodal/portfoliobuymodal.component';
import { PortfoliosellmodalComponent } from '../portfoliosellmodal/portfoliosellmodal.component';
import { NgbAlert } from '@ng-bootstrap/ng-bootstrap';
import { PortfolioSharedService } from '../portfolio-shared-service.service';
import { IntervalService } from '../interval.service';
import { BuybuttonclickedService } from '../buybuttonclicked.service';
import { SellbuttonclickedService } from '../sellbuttonclicked.service';

@Component({
  selector: 'app-portfolio',
  templateUrl: './portfolio.component.html',
  styleUrls: ['./portfolio.component.css']
})

export class PortfolioComponent implements OnInit {

  @ViewChild('selfClosingAlert', { static: false }) selfClosingAlert!: NgbAlert;

  portfolioData: any[] = [];
  moneyInWallet!: number;
  loadingPortfolio : boolean = true;
  loadingWalletBalance : boolean = true;
  alertType !: string;
  statusMessage !: string;
  buyOrNot!: boolean;

  setAlertMessage(message: string): void {
    this.statusMessage = message;
    setTimeout(() => {
      this.selfClosingAlert.close();
    }, 5000); 
  }

  // oldQuantity !: number;

  constructor(public sellbuttonClickedService : SellbuttonclickedService, private buybuttonclickedService : BuybuttonclickedService,private intervalService: IntervalService, private portfolioSharedService: PortfolioSharedService, private portfolioService: PortfolioService, public homeComponent: HomeComponent, private http: HttpClient, @Inject(NgbModal) private modalService: NgbModal) {}

  ngOnInit(): void {
    this.fetchPortfolioData();
    this.fetchBalance();
  }

  portfolioBuyModal(symbol: string, currentPrice: number) {

    const portfolioBuyModalRef = this.modalService.open(PortfoliobuymodalComponent);
    portfolioBuyModalRef.componentInstance.symbol = symbol;
    portfolioBuyModalRef.componentInstance.currentPrice = currentPrice;
    portfolioBuyModalRef.componentInstance.balanceWallet = this.moneyInWallet;

    portfolioBuyModalRef.closed.subscribe((result: any) => {
      if (this.buybuttonclickedService.buyButtonClicked == true) {
        this.fetchPortfolioData();
        this.fetchBalance();
        this.fetchPortfolioData();
        this.fetchBalance();
        this.setAlertMessage(`${symbol} bought successfully.`);
        this.alertType = 'success';
      }
    })

  }
  

//   portfolioBuyModal(symbol: string, currentPrice: number) {
//     let closedByCloseButton = false;

//     const portfolioBuyModalRef = this.modalService.open(PortfoliobuymodalComponent);
//     portfolioBuyModalRef.componentInstance.symbol = symbol;
//   portfolioBuyModalRef.componentInstance.currentPrice = currentPrice;
//   portfolioBuyModalRef.componentInstance.balanceWallet = this.moneyInWallet;
//   portfolioBuyModalRef.dismissed.subscribe(() => {
//     closedByCloseButton = true; // Set the flag to true when the modal is closed by the close button
//   });
//   portfolioBuyModalRef.closed.subscribe((result: any) => {
//  if (closedByCloseButton === false) {
//     this.setAlertMessage(`${symbol} bought successfully.`);
//     this.alertType = 'success';
//   }
//   })
//   portfolioBuyModalRef.hidden.subscribe(() => {
//     closedByCloseButton = false; // Reset the flag when the modal is hidden
//   });
//   }

  openSearchDetails(symbol: string): void {
    this.homeComponent.company = null;
    this.homeComponent.suggestions = [];
    this.homeComponent.searchResults = [];
    this.homeComponent.symbol = '';
    clearInterval(this.intervalService.intervalID);
    this.homeComponent.searchResultsService.storeSearchResults([]);
    console.log(symbol);
    this.homeComponent.symbol = symbol
    this.homeComponent.search();
  }

  async openSellModal(symbol: string, currentPrice: number, oldQuantity: number) {
    // console.log(company);
    const sellModalRef = this.modalService.open(PortfoliosellmodalComponent);
    sellModalRef.componentInstance.symbol = symbol;
    sellModalRef.componentInstance.currentPrice = currentPrice;
    sellModalRef.componentInstance.balanceWallet = this.moneyInWallet;
    sellModalRef.componentInstance.oldQuantity = oldQuantity;

    sellModalRef.closed.subscribe((result:any) => {
    if (this.sellbuttonClickedService.sellButtonClicked = true) {
      this.fetchPortfolioData();
      this.fetchBalance();
      this.fetchPortfolioData();
      this.fetchBalance();
      this.setAlertMessage(`${symbol} sold successfully.`);
      this.alertType = 'danger';
      }
    })




    // sellModalRef.closed.subscribe((result: any) => {
    //   this.setAlertMessage(`${symbol} sold successfully.`);
    //   this.alertType = 'danger';
    // });
  }

  fetchBalance(): void {
    this.http.get<any>('http://localhost:3000/balance').subscribe({
      next: (data) => {
        this.moneyInWallet = data.balance;
        // this.loadingWalletBalance = false;
      },
      error: (error) => {
        console.error('Error fetching balance:', error);
      }
    });

    // this.http.get<any>('/balance').subscribe({
    //   next: (data) => {
    //     this.moneyInWallet = data.balance;
    //     // this.loadingWalletBalance = false;
    //   },
    //   error: (error) => {
    //     console.error('Error fetching balance:', error);
    //   }
    // });
  }

  fetchPortfolioData(): void {
    this.portfolioService.getPortfolioData().subscribe(data => {
      this.portfolioData = data;
      this.loadingPortfolio = false;
    });
  }

}
