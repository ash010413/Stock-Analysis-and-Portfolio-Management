import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
  })

  export class PortfolioService {
    private baseUrl = 'http://localhost:3000'; // Base URL of your backend server
  
    constructor(private http: HttpClient) {}

   
  
    getPortfolioData(): Observable<any[]> {
      return this.http.get<any[]>(`${this.baseUrl}/portfolio`);
      // return this.http.get<any[]>(`/portfolio`);
    }
  }
