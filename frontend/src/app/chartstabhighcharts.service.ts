import { Injectable } from '@angular/core';
import * as Highcharts from 'highcharts/highstock';

import HC_indicators from 'highcharts/indicators/indicators'
HC_indicators(Highcharts);

import HC_volumebyprice from 'highcharts/indicators/volume-by-price';
HC_volumebyprice(Highcharts);

@Injectable({
  providedIn: 'root'
})
export class ChartsTabHighChartsService {
  Highcharts: typeof Highcharts = Highcharts;
  SMAandVolumeChartOptions: Highcharts.Options = {};
//   chartOptions: Highcharts.Options = {}; // Define your chart options here

  constructor() { }
}