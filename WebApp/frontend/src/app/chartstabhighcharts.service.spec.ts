import { TestBed } from '@angular/core/testing';

import { ChartstabhighchartsService } from './chartstabhighcharts.service';

describe('ChartstabhighchartsService', () => {
  let service: ChartstabhighchartsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ChartstabhighchartsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
