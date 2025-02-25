import { TestBed } from '@angular/core/testing';

import { CompanydataService } from './companydata.service';

describe('CompanydataService', () => {
  let service: CompanydataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CompanydataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
