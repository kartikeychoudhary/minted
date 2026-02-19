import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServerSettings } from './server-settings';

describe('ServerSettings', () => {
  let component: ServerSettings;
  let fixture: ComponentFixture<ServerSettings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServerSettings]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ServerSettings);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
