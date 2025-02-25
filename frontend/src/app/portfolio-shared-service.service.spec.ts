import { TestBed } from '@angular/core/testing';

import { PortfolioSharedServiceService } from './portfolio-shared-service.service';

describe('PortfolioSharedServiceService', () => {
  let service: PortfolioSharedServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PortfolioSharedServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
