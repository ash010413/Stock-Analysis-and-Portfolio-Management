import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PortfoliosellmodalComponent } from './portfoliosellmodal.component';

describe('PortfoliosellmodalComponent', () => {
  let component: PortfoliosellmodalComponent;
  let fixture: ComponentFixture<PortfoliosellmodalComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PortfoliosellmodalComponent]
    });
    fixture = TestBed.createComponent(PortfoliosellmodalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
