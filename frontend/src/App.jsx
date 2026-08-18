import { Navigate, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import MarketplacePage from './pages/MarketplacePage'
import NearbyListingsPage from './pages/NearbyListingsPage'
import MyListingsPage from './pages/MyListingsPage'
import ListingDetailPage from './pages/ListingDetailPage'
import ListingFormPage from './pages/ListingFormPage'
import CompanyProfilePage from './pages/CompanyProfilePage'
import ImpactReportPage from './pages/ImpactReportPage'
import ConversationsPage from './pages/ConversationsPage'
import ConversationThreadPage from './pages/ConversationThreadPage'
import NotFoundPage from './pages/NotFoundPage'
import { useAuth } from './context/AuthContext'

export default function App() {
  const { user } = useAuth()

  return (
    <>
      <Navbar />
      <main className="container">
        <Routes>
          <Route path="/login" element={user ? <Navigate to="/" replace /> : <LoginPage />} />
          <Route path="/register" element={user ? <Navigate to="/" replace /> : <RegisterPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<MarketplacePage />} />
            <Route path="/listings/nearby" element={<NearbyListingsPage />} />
            <Route path="/listings/mine" element={<MyListingsPage />} />
            <Route path="/listings/new" element={<ListingFormPage />} />
            <Route path="/listings/:id" element={<ListingDetailPage />} />
            <Route path="/listings/:id/edit" element={<ListingFormPage />} />
            <Route path="/company" element={<CompanyProfilePage />} />
            <Route path="/impact-report" element={<ImpactReportPage />} />
            <Route path="/conversations" element={<ConversationsPage />} />
            <Route path="/conversations/:id" element={<ConversationThreadPage />} />
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </>
  )
}
