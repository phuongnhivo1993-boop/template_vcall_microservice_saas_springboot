import type { NextAuthOptions } from 'next-auth';
import CredentialsProvider from 'next-auth/providers/credentials';
import axios from 'axios';

export const authOptions: NextAuthOptions = {
  providers: [
    CredentialsProvider({
      name: 'Credentials',
      credentials: {
        username: { label: 'Username', type: 'text' },
        password: { label: 'Password', type: 'password' },
        accessToken: { label: 'Access Token', type: 'text' },
        refreshToken: { label: 'Refresh Token', type: 'text' },
        userRole: { label: 'User Role', type: 'text' },
        userId: { label: 'User ID', type: 'text' },
        userEmail: { label: 'User Email', type: 'text' },
      },
      async authorize(credentials) {
        try {
          if (credentials?.accessToken) {
            return {
              id: credentials.userId || 'mfa-user',
              name: credentials.username || '',
              email: credentials.userEmail || '',
              role: credentials.userRole || 'AGENT',
              accessToken: credentials.accessToken,
              refreshToken: credentials.refreshToken || '',
            };
          }

          const res = await axios.post('http://localhost:8080/api/v1/auth/login', {
            username: credentials?.username,
            password: credentials?.password,
          });

          const data = res.data;
          if (data.user && data.accessToken) {
            return {
              id: data.user.id,
              name: data.user.username,
              email: data.user.email,
              role: data.user.role,
              accessToken: data.accessToken,
              refreshToken: data.refreshToken,
            };
          }
          return null;
        } catch {
          return null;
        }
      },
    }),
  ],
  callbacks: {
    async jwt({ token, user }) {
      if (user) {
        token.accessToken = user.accessToken;
        token.refreshToken = user.refreshToken;
        token.role = user.role;
      }
      return token;
    },
    async session({ session, token }) {
      session.accessToken = token.accessToken as string;
      session.user.role = token.role as string;
      return session;
    },
  },
  pages: {
    signIn: '/auth/login',
    error: '/auth/login',
  },
  session: {
    strategy: 'jwt',
    maxAge: 24 * 60 * 60,
  },
  secret: process.env.NEXTAUTH_SECRET || 'vcall-secret-change-in-production',
};
