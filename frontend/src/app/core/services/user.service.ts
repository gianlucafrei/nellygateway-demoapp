import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable ,  BehaviorSubject ,  ReplaySubject } from 'rxjs';

import { ApiService } from './api.service';
import { JwtService } from './jwt.service';
import { User } from '../models';
import { map ,  distinctUntilChanged } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';


@Injectable()
export class UserService {
  private currentUserSubject = new BehaviorSubject<User>({} as User);
  public currentUser = this.currentUserSubject.asObservable().pipe(distinctUntilChanged());

  private isAuthenticatedSubject = new ReplaySubject<boolean>(1);
  public isAuthenticated = this.isAuthenticatedSubject.asObservable();
  private registartionInfo;

  constructor (
    private apiService: ApiService,
    private http: HttpClient,
    private jwtService: JwtService,
    private router: Router
  ) {}

  // Verify JWT in localstorage with server & load user's info.
  // This runs once on application startup.
  populate() {
    console.log("populate()")
    // If JWT detected, attempt to get & store user's info
    this.apiService.get('/user')
      .subscribe(
        data => {
          
          if(data.needsRegistration)
          {
            console.log("User needs registration");
            this.isAuthenticatedSubject.next(false);
            this.setRegistrationInfo(data);
            this.router.navigateByUrl('/register');
          }
          else
          {
            this.setAuth(data.user)
          }
        },
        err => this.purgeAuth()
      );
  }

  setRegistrationInfo(info){
    this.registartionInfo = info;
  }

  getRegistrationInfo(){
    return this.registartionInfo;
  }

  setAuth(user: User) {
    console.log("setAuth()")
    // Save JWT sent from server in localstorage
    this.jwtService.saveToken(user.token);
    // Set current user data into observable
    this.currentUserSubject.next(user);
    // Set isAuthenticated to true
    this.isAuthenticatedSubject.next(true);
  }

  purgeAuth() {
    console.log("purgeAuth()")
    // Set current user to an empty object
    this.currentUserSubject.next({} as User);
    // Set auth status to false
    this.isAuthenticatedSubject.next(false);
  }

  attemptAuth(type, credentials): Observable<User> {
    console.log("purgeAuth()")
    const route = (type === 'login') ? '/login' : '';
    return this.apiService.post('/users' + route, {user: credentials})
      .pipe(map(
      data => {
        this.setAuth(data.user);
        return data;
      }
    ));
  }

  getCurrentUser(): User {
    console.log("getCurrentUser()")
    return this.currentUserSubject.value;
  }

  // Update the user on the server (email, pass, etc)
  update(user): Observable<User> {
    console.log("update(user)")
    return this.apiService
    .put('/user', { user })
    .pipe(map(data => {
      // Update the currentUser observable
      this.currentUserSubject.next(data.user);
      return data.user;
    }));
  }

}
