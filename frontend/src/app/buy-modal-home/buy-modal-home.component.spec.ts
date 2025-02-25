import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BuyModalHomeComponent } from './buy-modal-home.component';

describe('BuyModalHomeComponent', () => {
  let component: BuyModalHomeComponent;
  let fixture: ComponentFixture<BuyModalHomeComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [BuyModalHomeComponent]
    });
    fixture = TestBed.createComponent(BuyModalHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
