export { default } from 'next-auth/middleware';

export const config = {
  matcher: ['/((?!auth|api/auth|api/v1|_next/static|_next/image|favicon.ico|logo.svg).*)'],
};
