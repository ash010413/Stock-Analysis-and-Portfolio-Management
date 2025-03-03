import { Injectable } from '@angular/core';
import * as Highcharts from 'highcharts';

@Injectable({
  providedIn: 'root'
})
export class HighchartsService {
  Highcharts: typeof Highcharts = Highcharts;
  chartOptions: Highcharts.Options = {}; // Define your chart options here

  constructor() { }
}
