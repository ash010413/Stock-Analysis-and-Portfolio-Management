import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PortfoliobuymodalComponent } from './portfoliobuymodal.component';

describe('PortfoliobuymodalComponent', () => {
  let component: PortfoliobuymodalComponent;
  let fixture: ComponentFixture<PortfoliobuymodalComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PortfoliobuymodalComponent]
    });
    fixture = TestBed.createComponent(PortfoliobuymodalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
