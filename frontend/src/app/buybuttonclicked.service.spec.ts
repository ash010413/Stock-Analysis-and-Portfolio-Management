import { TestBed } from '@angular/core/testing';

import { BuybuttonclickedService } from './buybuttonclicked.service';

describe('BuybuttonclickedService', () => {
  let service: BuybuttonclickedService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BuybuttonclickedService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
