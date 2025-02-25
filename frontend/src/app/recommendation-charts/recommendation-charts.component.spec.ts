import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecommendationChartsComponent } from './recommendation-charts.component';

describe('RecommendationChartsComponent', () => {
  let component: RecommendationChartsComponent;
  let fixture: ComponentFixture<RecommendationChartsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RecommendationChartsComponent]
    });
    fixture = TestBed.createComponent(RecommendationChartsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
