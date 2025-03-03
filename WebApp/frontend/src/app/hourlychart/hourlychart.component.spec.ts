import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HourlychartComponent } from './hourlychart.component';

describe('HourlychartComponent', () => {
  let component: HourlychartComponent;
  let fixture: ComponentFixture<HourlychartComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [HourlychartComponent]
    });
    fixture = TestBed.createComponent(HourlychartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
