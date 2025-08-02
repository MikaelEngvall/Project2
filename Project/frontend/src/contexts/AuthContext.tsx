// ========================================
// DFRM - AuthContext för OAuth2-autentisering
// ========================================

'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { api } from '@/lib/api-client';

// Användartyper
export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: 'USER' | 'ADMIN' | 'SUPERADMIN';
  preferredLanguage: string;
  isActive: boolean;
  permissions: string[];
  phone?: string;
  createdAt: string;
  updatedAt: string;
}

// Auth state interface
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

// Auth context interface
interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<void>;
  clearError: () => void;
}

// Skapa context
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Auth provider props
interface AuthProviderProps {
  children: React.ReactNode;
}

// Auth provider komponent
export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [state, setState] = useState<AuthState>({
    user: null,
    isAuthenticated: false,
    isLoading: true,
    error: null,
  });

  // Hjälpmetod för att sätta auth token i cookie
  const setAuthToken = (token: string) => {
    if (typeof document !== 'undefined') {
      // Säker HttpOnly cookie (server-side)
      document.cookie = `auth_token=${token}; path=/; secure; samesite=strict; max-age=43200`;
    }
  };

  // Hjälpmetod för att ta bort auth token
  const removeAuthToken = () => {
    if (typeof document !== 'undefined') {
      document.cookie = 'auth_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
    }
  };

  // Hjälpmetod för att hämta användardata från token
  const getUserFromToken = async (token: string): Promise<User | null> => {
    try {
      // I en riktig implementation skulle vi validera token på servern
      // För nu returnerar vi mock-data
      return {
        id: '1',
        firstName: 'Admin',
        lastName: 'User',
        email: 'admin@duggalsfastigheter.se',
        role: 'SUPERADMIN',
        preferredLanguage: 'sv',
        isActive: true,
        permissions: ['*'],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
    } catch (error) {
      console.error('Fel vid hämtning av användardata:', error);
      return null;
    }
  };

  // Kontrollera om användare är autentiserad vid app-start
  useEffect(() => {
    const checkAuthStatus = async () => {
      try {
        // Kontrollera om det finns en token i cookies
        const token = document.cookie
          .split('; ')
          .find(row => row.startsWith('auth_token='))
          ?.split('=')[1];

        if (token) {
          const user = await getUserFromToken(token);
          if (user) {
            setState({
              user,
              isAuthenticated: true,
              isLoading: false,
              error: null,
            });
          } else {
            // Ogiltig token, ta bort den
            removeAuthToken();
            setState({
              user: null,
              isAuthenticated: false,
              isLoading: false,
              error: null,
            });
          }
        } else {
          setState({
            user: null,
            isAuthenticated: false,
            isLoading: false,
            error: null,
          });
        }
      } catch (error) {
        console.error('Fel vid kontroll av auth status:', error);
        setState({
          user: null,
          isAuthenticated: false,
          isLoading: false,
          error: 'Ett fel uppstod vid autentisering',
        });
      }
    };

    checkAuthStatus();
  }, []);

  // Login-funktion
  const login = async (email: string, password: string) => {
    try {
      setState(prev => ({ ...prev, isLoading: true, error: null }));

      // Anropa login API
      const response = await api.auth.login({ email, password });
      
      // Sätt token i cookie
      setAuthToken(response.token);

      // Hämta användardata
      const user = await getUserFromToken(response.token);
      
      if (user) {
        setState({
          user,
          isAuthenticated: true,
          isLoading: false,
          error: null,
        });
      } else {
        throw new Error('Kunde inte hämta användardata');
      }
    } catch (error: any) {
      console.error('Login-fel:', error);
      setState(prev => ({
        ...prev,
        isLoading: false,
        error: error.message || 'Inloggning misslyckades',
      }));
      throw error;
    }
  };

  // Logout-funktion
  const logout = async () => {
    try {
      // Anropa logout API
      await api.auth.logout();
    } catch (error) {
      console.error('Logout API-fel:', error);
    } finally {
      // Ta bort token från cookie
      removeAuthToken();
      
      // Uppdatera state
      setState({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      });
    }
  };

  // Refresh token-funktion
  const refreshToken = async () => {
    try {
      const response = await api.auth.refresh();
      setAuthToken(response.token);
      
      // Hämta uppdaterad användardata
      const user = await getUserFromToken(response.token);
      if (user) {
        setState(prev => ({ ...prev, user }));
      }
    } catch (error) {
      console.error('Token refresh-fel:', error);
      // Om refresh misslyckas, logga ut användaren
      await logout();
    }
  };

  // Rensa felmeddelande
  const clearError = () => {
    setState(prev => ({ ...prev, error: null }));
  };

  // Context value
  const contextValue: AuthContextType = {
    ...state,
    login,
    logout,
    refreshToken,
    clearError,
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

// Hook för att använda auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth måste användas inom en AuthProvider');
  }
  return context;
};

// Hook för att kontrollera behörigheter
export const usePermissions = () => {
  const { user } = useAuth();
  
  const hasPermission = (permission: string): boolean => {
    if (!user) return false;
    
    // SUPERADMIN har alla behörigheter
    if (user.role === 'SUPERADMIN') return true;
    
    // Kontrollera specifika behörigheter
    return user.permissions.includes(permission) || user.permissions.includes('*');
  };

  const hasRole = (role: string): boolean => {
    if (!user) return false;
    return user.role === role;
  };

  return {
    hasPermission,
    hasRole,
    user,
  };
};

export default AuthContext; 