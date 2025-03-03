import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
// import { SearchComponent } from './search/search.component';
import { WatchlistComponent } from './watchlist/watchlist.component'; // Import the WatchlistComponent
import { PortfolioComponent } from './portfolio/portfolio.component';

const routes: Routes = [
  { path: '', redirectTo: '/search/home', pathMatch: 'full' }, // Redirect empty path to '/search/home'
  {path: 'home', redirectTo: '/search/home', pathMatch: 'full'},
  { path: 'search/home', component: HomeComponent }, // Define other routes as needed
  { path: 'search/:symbol', component: HomeComponent },
  { path: 'watchlist', component: WatchlistComponent }, // Define route for /watchlist
  { path: 'portfolio', component: PortfolioComponent },
  {path: '**', redirectTo: '/search/home'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }