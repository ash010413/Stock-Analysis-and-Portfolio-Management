import { TestBed } from '@angular/core/testing';

import { SellbuttonclickedService } from './sellbuttonclicked.service';

describe('SellbuttonclickedService', () => {
  let service: SellbuttonclickedService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SellbuttonclickedService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
