// import { NgModule } from '@angular/core';
// import { BrowserModule } from '@angular/platform-browser';

// import { AppRoutingModule } from './app-routing.module';
// import { AppComponent } from './app.component';
// import { NavbarComponent } from './navbar/navbar.component';
// import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
// import { HomeComponent } from './home/home.component';
// import { NewsModalComponent } from './news-modal/news-modal.component';
// import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
// import { BuyModalComponent } from './buy-modal/buy-modal.component';
// import { PortfolioComponent } from './portfolio/portfolio.component';
// import { PortfoliobuymodalComponent } from './portfoliobuymodal/portfoliobuymodal.component';
// import { PortfoliosellmodalComponent } from './portfoliosellmodal/portfoliosellmodal.component';
// import { BuyModalHomeComponent } from './buy-modal-home/buy-modal-home.component';

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { NavbarComponent } from './navbar/navbar.component';
import { AppRoutingModule } from './app-routing.module';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
// import { SearchComponent } from './search/search.component';
import { SearchResultsService } from './search-results.service';
import { CompanydataService } from './companydata.service';
import { WatchlistComponent } from './watchlist/watchlist.component';
import { PortfolioComponent } from './portfolio/portfolio.component';
import { NewsModalComponent } from './news-modal/news-modal.component';
import { HighchartsChartModule } from 'highcharts-angular';
import { RecommendationChartsComponent } from './recommendation-charts/recommendation-charts.component';
import { EarningsChartComponent } from './earnings-chart/earnings-chart.component';
import { HourlychartComponent } from './hourlychart/hourlychart.component';
import { ChartsComponent } from './charts/charts.component';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatInputModule } from '@angular/material/input';
import { MatTabsModule } from '@angular/material/tabs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
// import { FooterComponent } from './footer/footer.component';
import { BuyModalComponent } from './buy-modal/buy-modal.component';
import { PortfoliobuymodalComponent } from './portfoliobuymodal/portfoliobuymodal.component';
import { PortfoliosellmodalComponent } from './portfoliosellmodal/portfoliosellmodal.component';
import { SellModalComponent } from './sell-modal/sell-modal.component';
import { HourlyChartComponent } from './hourly-chart/hourly-chart.component';
import { BuyModalHomeComponent } from './buy-modal-home/buy-modal-home.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    NavbarComponent,
    WatchlistComponent,
    PortfolioComponent,
    NewsModalComponent,
    RecommendationChartsComponent,
    EarningsChartComponent,
    HourlychartComponent,
    ChartsComponent,
    BuyModalComponent,
    PortfoliobuymodalComponent,
    PortfoliosellmodalComponent,
    SellModalComponent,
    HourlyChartComponent,
    BuyModalHomeComponent
  ],
  imports: [
    BrowserModule,
    NgbModule,
    HttpClientModule,
    FormsModule,
    AppRoutingModule,
    MatProgressSpinnerModule,
    HighchartsChartModule,
    MatAutocompleteModule,
    MatInputModule,
    MatTabsModule,
    BrowserAnimationsModule
  ],
  providers: [SearchResultsService, CompanydataService, HomeComponent, PortfolioComponent],
  bootstrap: [AppComponent]
})
export class AppModule { }
